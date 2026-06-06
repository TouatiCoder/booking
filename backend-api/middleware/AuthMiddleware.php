<?php
require_once '../config/jwt.php';
require_once '../helpers/ResponseHelper.php';

function authenticate() {
    $headers = apache_request_headers();
    if(isset($headers['Authorization'])) {
        $token = str_replace('Bearer ', '', $headers['Authorization']);
        $decoded = JWT::decode($token, JWT_SECRET);
        if($decoded) {
            return $decoded;
        }
    }
    sendResponse(401, "Unauthorized");
}
?>
