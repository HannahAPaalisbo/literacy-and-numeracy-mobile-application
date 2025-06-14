<?php
header('Content-Type: application/json');

require 'connection.php';

try {
    $data = array();
    $tableName = "tbl_lesson";
    $query = "SELECT * FROM " . $tableName . " WHERE lesson_status = 1 ORDER BY lesson_name ";
    $result = mysqli_query($db_con, $query);

    $subjectId = "";
    $subject = "";
    $category = "";
    
    $item = array(
        "subjectId" => $subjectId,
        "subject" => $subject,
        "category" => $category
    );

    while (($row = mysqli_fetch_assoc($result))) {
        $item["subjectId"] = $row['lesson_id'];
        $item["subject"] = $row['lesson_name'];
        $categoryId = $row['category_id'];
        $tableName = "tbl_category";
        $querya = "SELECT * FROM " . $tableName . " WHERE category_id = '" . $categoryId ."'";
        $resulta = mysqli_query($db_con, $querya);
        $row2 = mysqli_fetch_assoc($resulta);
        $category = $row2['category_name'];
        $item["category"] = trim($category);
    
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
