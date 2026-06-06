<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
require_once '../config/database.php';
require_once '../helpers/ResponseHelper.php';
require_once '../config/jwt.php';

$data = json_decode(file_get_contents("php://input"));
if(!isset($data->email) || !isset($data->password)) {
    sendResponse(400, "Missing credentials");
}

// In a real app we'd query the DB here and verify password
if($data->email == "test@test.com" && $data->password == "password") {
    $token = JWT::encode(["email" => $data->email], JWT_SECRET);
    sendResponse(200, "Login successful", ["token" => $token]);
} else {
    sendResponse(401, "Invalid credentials");
}
?>
