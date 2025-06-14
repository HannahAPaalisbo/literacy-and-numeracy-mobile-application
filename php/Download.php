<!DOCTYPE html>
<html>
<head>
    <title>APK Download</title>
</head>
<body>
    <form method="post">
        <input type="submit" name="download" value="Download Me!" />
    </form>

    <?php
    if (isset($_POST['download'])) {
        $fileName = 'TTLN_app.apk';
        $filePath = 'https://www.tagakauloedu.com/Mobile/' . $fileName;

        // Set the appropriate headers for APK file download
        header('Content-Type: application/vnd.android.package-archive');
        header("Content-Disposition: attachment; filename=\"$fileName\"");
        header('Content-Length: ' . filesize($filePath));

        // Output the APK file
        readfile($filePath);
        exit;
    }
    ?>
</body>
</html>
