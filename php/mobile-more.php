<?php
$host = "localhost";
$user = "root";
$password = "";
$database = "u170333284_db_tagakaulo";

$connection = mysqli_connect($host, $user, $password, $database);

if (!$connection) {
    die("Connection failed: " . mysqli_connect_error());
}

?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leadership Board & Badges</title>
    <style>
        .sub-leadership-board-container {
            border-collapse: collapse;
            width: 100%;
            margin-top: 20px;
        }

        .sub-leadership-board-container,
        .sub-leadership-board-container th,
        .sub-leadership-board-container td {
            border: 1px solid black;
        }

        .sub-leadership-board-container th,
        .sub-leadership-board-container td {
            padding: 10px;
            text-align: left;
        }
    </style>
</head>

<body>

<?php
// Get the personal_id from the URL parameter
$personalId = $_GET['personal_id'];

// Fetch full name using personal_id as a reference
$fullNameQuery = "SELECT CONCAT(first_name, ' ', middle_name, ' ', last_name) AS full_name FROM tbl_user_info WHERE personal_id = '$personalId'";
$fullNameResult = mysqli_query($connection, $fullNameQuery);
$fullNameRow = mysqli_fetch_assoc($fullNameResult);
$fullName = $fullNameRow['full_name'];

?>

<h2><?php echo $fullName; ?>'s Details</h2>

<table class="sub-leadership-board-container">
    <tr>
        <th>Total Quiz Score</th>
        <th>Story Point</th>
        <th>Assignment Score</th>
        <th>Overal Total Score</th>
        <th>Attendance</th>
        <th>Overall Total Score wida twist</th>
    </tr>

    <?php
    // Fetch the sum of all quiz scores
    $totalQuizScoreQuery = "SELECT COALESCE(SUM(CAST(score AS DECIMAL(5, 2))), 0.00) AS total_quiz_score FROM tbl_learner_quiz_progress WHERE learner_id = '$personalId'";
    $totalQuizScoreResult = mysqli_query($connection, $totalQuizScoreQuery);
    $quiz_progress = mysqli_fetch_assoc($totalQuizScoreResult);
    $quiz_progress = $quiz_progress['total_quiz_score'];

    // Fetch the total count of story points
    $totalStoryPointsQuery = "SELECT COUNT(DISTINCT story_id) AS total_story_points FROM tbl_learner_story_progress WHERE learner_id = '$personalId'";
    $totalStoryPointsResult = mysqli_query($connection, $totalStoryPointsQuery);
    $story_progress = mysqli_fetch_assoc($totalStoryPointsResult);
    $story_progress = $story_progress['total_story_points'];

    // Fetch the sum of all assignment scores
    $totalAssignmentScoreQuery = "SELECT COALESCE(SUM(CAST(score AS DECIMAL(5, 2))), 0.00) AS total_assignment_score FROM tbl_learner_assignment_progress WHERE learner_id = '$personalId'";
    $totalAssignmentScoreResult = mysqli_query($connection, $totalAssignmentScoreQuery);
    $assignment_progress = mysqli_fetch_assoc($totalAssignmentScoreResult);
    $assignment_progress = $assignment_progress['total_assignment_score'];

    $totalScore = $assignment_progress + $story_progress + $quiz_progress;

    // Query para makuha ang total score sa quiz / story points / assignment scores then idivide nalang nato para makuha ang result
    $storytotalQuery = "SELECT COUNT(topic_id) AS total_topics FROM tbl_topic WHERE topic_status = 1;";
    $storytotalResult = mysqli_query($connection, $storytotalQuery);
    $storytotalRow = mysqli_fetch_assoc($storytotalResult);
    $totalStoryTopics = $storytotalRow['total_topics'];

    $quizTotalQuery = "SELECT SUM(score) AS total_quiz_score FROM tbl_quiz WHERE quiz_status = 1;";
    $quizTotalResult = mysqli_query($connection, $quizTotalQuery);
    $quizTotalRow = mysqli_fetch_assoc($quizTotalResult);
    $totalQuizSum = $quizTotalRow['total_quiz_score'];

    $assignmentTotalQuery = "SELECT SUM(max_score) AS total_assignment_score FROM tbl_assignment WHERE status = 1;";
    $assignmentTotalResult = mysqli_query($connection, $assignmentTotalQuery);
    $assignmentTotalRow = mysqli_fetch_assoc($assignmentTotalResult);
    $totalAssignmentSum = $assignmentTotalRow['total_assignment_score'];

    $totalSum = $totalStoryTopics + $totalQuizSum + $totalQuizSum;

    $totalResult = $totalScore / $totalSum;

    echo "<tr>
            <td>$quiz_progress</td>
            <td>$story_progress</td>
            <td>$assignment_progress</td>
            <td>$totalScore</td>
            <td>$story_progress</td>
            <td>$totalResult</td>
          </tr>";
    ?>

</table>

<form class="back-button-form" action="lbab.php" method="get">
    <button class="back-button" type="submit">Back to Leadership Board & Badges</button>
</form>
</body>

</html>

<?php
mysqli_close($connection);
?>
