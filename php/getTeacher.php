<?php
header('Content-Type: application/json');

require 'connection.php';

try {
    $data = array();
    
    $tableName = "tbl_user_info";
    $query = "SELECT * FROM " . $tableName . " WHERE user_level_id = '1' AND status_id = '1'";
    $result = mysqli_query($db_con, $query);

    $teacherId = "";
    $firstName = "";
    $lastName = "";

    $item = array(
        "teacherId" => $teacherId,
        "firstName" => $firstName,
        "lastName" => $lastName
    );

    while($row = mysqli_fetch_assoc($result)) {
        $item["teacherId"] = $row['personal_id'];
        $item["firstName"] = $row['first_name'];
        $item["lastName"] = $row['last_name'];
        $data[] = $item;
    }                     

    $jsonData = json_encode($data);
    echo $jsonData;
} catch (Exception $e) {
    $response = "$e";
    echo json_encode($response);
}
mysqli_close($db_con)
?>
