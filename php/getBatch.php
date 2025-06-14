<?php
header('Content-Type: application/json');

require 'connection.php';

try {
    $data = array();
    
    $tableName = "tbl_schoolyear";
    $query = "SELECT * FROM " . $tableName . " WHERE sy_status = '1' ORDER BY sy_start";
    $result = mysqli_query($db_con, $query);

    $syId = "";
    $syStart = "";
    $syEnd = "";

    $item = array(
        "syId" => $languageId,
        "syStart" => $kalagan,
        "syEnd" => $filipino,
    );

    while($row = mysqli_fetch_assoc($result)) {
        $item["syId"] = $row['sy_id'];
        $item["syStart"] = $row['sy_start'];
        $item["syEnd"] = $row['sy_end'];
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
