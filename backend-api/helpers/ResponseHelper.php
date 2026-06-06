<?php
function sendResponse($code, $message, $data = null) {
    http_response_code($code);
    echo json_encode(['success' => $code >= 200 && $code < 300, 'message' => $message, 'data' => $data]);
    exit();
}
?>
