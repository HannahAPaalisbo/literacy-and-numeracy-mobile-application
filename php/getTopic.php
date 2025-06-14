<?php
header('Content-Type: application/json');

require 'connection.php';

try {
    $data = array();
    $tableName = "tbl_topic";
    $query = "SELECT * FROM " . $tableName . " WHERE topic_status = 1 ORDER BY lesson_id ASC;";
    $result = mysqli_query($db_con, $query);

    $subjectId = "";
    $subject = "";
    $subjectIdRef = "";
    $imageId = "";
    $audioId = "";
    $videoId = "";
    $pdfPath = "";

    while (($row = mysqli_fetch_assoc($result))) {
        $topicId = $row['topic_id'];
        
        $item = array(
        "topicId" => $topicId,
        "topic" => $topic,
        "subjectIdRef" => $subjectIdRef,
        "imagePath" => $imageId,
        "audioPath" => $audioId,
        "videoPath" => $videoId,
        "pdfPath" => $pdfPath
        );
        
        $item["topic"] = $row['topic_name'];
        $item["subjectIdRef"] = $row['lesson_id'];
        
        $aquery = "SELECT * FROM tbl_image WHERE topic_id = ?;";
        $stmt = mysqli_prepare($db_con, $aquery);
        $param = $topicId;
        mysqli_stmt_bind_param($stmt, "s", $param);
        mysqli_stmt_execute($stmt);
        $result2 = mysqli_stmt_get_result($stmt);
        mysqli_stmt_close($stmt);
    
        while ($row2 = mysqli_fetch_assoc($result2)) {
            $item["imagePath"] = $row2['image_path'];
        }
    
        $bquery = "SELECT * FROM tbl_video WHERE topic_id = ?;";
        $stmt = mysqli_prepare($db_con, $bquery);
        $param = $topicId;
        mysqli_stmt_bind_param($stmt, "s", $param);
        mysqli_stmt_execute($stmt);
        $result3 = mysqli_stmt_get_result($stmt);
        mysqli_stmt_close($stmt);
    
        while ($row3 = mysqli_fetch_assoc($result3)) {
            $item["videoPath"] = $row3['video_path'];
        }
        
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
