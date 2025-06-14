<?php
header('Content-Type: application/json');

require 'connection.php';

try {
    $data = array();
    $tableName = "tbl_games";
    $query = "SELECT * FROM " . $tableName;
    $result = mysqli_query($db_con, $query);

    while ($row = mysqli_fetch_assoc($result)) {
        $item = array(
            "gameId" => $row['games_id'] ?? "",
            "answer" => $row['answer'] ?? "",
            "question" => $row['description'] ?? "",
            "hint" => $row['hint'] ?? "",
            "type" => $row['type'] ?? "",
            "imagePath1" => $row['image_path_1'] ?? "",
            "imagePath2" => $row['image_path_2'] ?? "",
            "audioPath" => $row['audio_path'] ?? "",
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
