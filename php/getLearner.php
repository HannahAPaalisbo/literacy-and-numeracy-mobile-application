<?php
header('Content-Type: application/json');

require 'connection.php';

try {
    $data = array();
    $tableName = "tbl_user_info";
    $query = "SELECT * FROM " . $tableName . " WHERE user_level_id = '2' AND status_id = '1'";
    $result = mysqli_query($db_con, $query);

    while ($row = mysqli_fetch_assoc($result)) {
        $item = array(
            "learnerId" => $row['personal_id'] ?? "",
            "firstName" => $row['first_name'] ?? "",
            "lastName" => $row['lastname_name'] ?? ""
        );

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
