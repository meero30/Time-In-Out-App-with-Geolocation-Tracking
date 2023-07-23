<?php
if(!empty($_POST['phone']) && !empty($_POST['apiKey'])){
    $phone = $_POST['phone'];
    $apiKey = $_POST['apiKey'];
    $result = array();
    $con = mysqli_connect("localhost", "root", "", "scworker");
    if($con){
        $sql = "select * from workers where phone = '".$phone."' and apiKey = '".$apiKey."'";
        $res = mysqli_query($con, $sql);
        if(mysqli_num_rows($res) != 0){
            $row = mysqli_fetch_assoc($res);
            $result = array("status" =>"success","message"=>"Data fetched successfully",
                "fullname"=> $row['fullname'],"email"=>$row['email'],"phone" => $row['phone'],"apiKey"=>$row['apiKey']);
        }else $result = array("status"=> "failed", "message"=> "Unauthorized access!");
    }else $result = array("status"=> "failed","message"=> "Database connection failed");
}else $result = array("status" => "failed","message"=> "All fields are required");

echo json_encode($result, JSON_PRETTY_PRINT);