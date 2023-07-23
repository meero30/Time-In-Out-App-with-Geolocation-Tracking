<?php
if(!empty($_POST['fullname']) && !empty($_POST['email']) && !empty($_POST['phone']) && !empty($_POST['password'])) {
    $con = mysqli_connect("localhost", "root", "", "scworker");
    $fullname = $_POST['fullname'];
    $email = $_POST['email'];
    $phone = $_POST['phone'];
    $password = password_hash($_POST['password'], PASSWORD_DEFAULT);
    if ($con) {
        $sql = "insert into workers (fullname,email,phone,password) values ('".$fullname."','".$email."','".$phone."','".$password."')";
        if(mysqli_query($con,$sql)){
            echo "success";
        }else echo "Registration failed";
    } else echo "Database connection failed";
}else echo "ALl fields are required";
