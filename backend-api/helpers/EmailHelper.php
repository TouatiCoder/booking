<?php
function sendResendEmail($to, $subject, $htmlContent) {
    $api_key = 're_123456789_placeholder'; // In reality this would come from ENV
    
    $ch = curl_init();
    
    $post_data = json_encode([
        "from" => "Zellige Stays <notifications@zellige.com>",
        "to" => [$to],
        "subject" => $subject,
        "html" => $htmlContent
    ]);
    
    curl_setopt($ch, CURLOPT_URL, 'https://api.resend.com/emails');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post_data);
    
    $headers = [];
    $headers[] = 'Authorization: Bearer ' . $api_key;
    $headers[] = 'Content-Type: application/json';
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    
    $result = curl_exec($ch);
    if (curl_errno($ch)) {
        error_log('Error sending Resend email: ' . curl_error($ch));
    }
    curl_close($ch);
    
    return $result;
}
?>
