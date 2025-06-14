<?php
$host = "156.67.222.1"; //change ip host here
$username = "u170333284_admin"; //change username
$password = "Capstone1!"; //change password
$database = "u170333284_db_tagakaulo"; //change database name if applicable

$db_con = mysqli_connect($host, $username, $password, $database);

if (mysqli_connect_errno()) {
    $response["error"] = true;
    $response["message"] = "Failed to connect to the database";
    echo json_encode($response);
    exit;
}
?>
