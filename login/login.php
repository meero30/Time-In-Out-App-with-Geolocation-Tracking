<?php
if(!empty($_POST['phone']) && !empty($_POST['password'])) {
    $phone = $_POST['phone'];
    $password = $_POST['password'];
    $result = array();
    $con = mysqli_connect("localhost", "root", "", "scworker");

    if ($con) {
        $sql = "select * from workers where phone = '".$phone."'";
        $res = mysqli_query($con, $sql);
        if(mysqli_num_rows($res)!=0){
            $row = mysqli_fetch_assoc($res);
            if($phone == $row['phone'] && password_verify($password,$row['password'])){
                try{
                    $apiKey = bin2hex(openssl_random_pseudo_bytes(23));
                }catch (Exception $e) {
                    $apiKey =bin2hex(uniqid($phone,true));
                }
                $sqlUpdate = "update workers set apiKey = '".$apiKey."' where phone = '".$phone."'";
                if (mysqli_query($con,$sqlUpdate)){
                    $result = array("status" =>"success","message"=>"Login successful",
                        "fullname"=> $row['fullname'],"email"=> $row['email'],"phone"=>$row['phone'],"apiKey"=>$apiKey);

                }else $result = array ("status" => "failed","message"=>"Login failed try again");
            }else $result = array("status"=> "failed","message"=>" Invalid Phone number or incorrect password!");
        }else $result = array("status"=> "failed", "message"=> "Invalid Phone number or incorrect password!");
    }else $result = array("status"=> "failed","message"=> "Database connection failed");
}else $result = array("status" => "failed","message"=> "All fields are required");
echo json_encode($result, JSON_PRETTY_PRINT);