<?php
header('Content-Type: application/json');

require 'connection.php';

try {
    $data = array();
    
    $tableName = "tbl_language";
    $query = "SELECT * FROM " . $tableName;
    $result = mysqli_query($db_con, $query);

    $languageId = "";
    $kalagan = "";
    $filipino = "";
    $english = "";
    $langTopicRef = "";

    $item = array(
        "languageId" => $languageId,
        "kalagan" => $kalagan,
        "filipino" => $filipino,
        "english" => $english,
        "langTopicRef" => $langTopicRef
    );

    while($row = mysqli_fetch_assoc($result)) {
        $item["languageId"] = $row['language_id'];
        $item["kalagan"] = $row['kalagan'];
        $item["filipino"] = $row['filipino'];
        $item["english"] = $row['english'];
        $item["langTopicRef"] = $row['topic_id'];
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
