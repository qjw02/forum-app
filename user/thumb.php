<?php
/**
 * App 图片缩略图与缓存接口
 * 部署路径：/www/wwwroot/qq/wwwroot/api/user/thumb.php
 */
$root = '/www/wwwroot/qq/wwwroot';
$relative = isset($_GET['path']) ? rawurldecode($_GET['path']) : '';
$width = isset($_GET['w']) ? intval($_GET['w']) : 480;
$width = max(120, min(1280, $width));

function output_original_image($file) {
    $info = @getimagesize($file);
    header('Content-Type: ' . ($info && !empty($info['mime']) ? $info['mime'] : 'application/octet-stream'));
    header('Cache-Control: public, max-age=2592000, immutable');
    header('X-Content-Type-Options: nosniff');
    readfile($file);
    exit;
}

if ($relative === '' || strpos($relative, 'data/attachment/') !== 0 ||
    strpos($relative, '..') !== false ||
    !preg_match('#^data/attachment/[A-Za-z0-9_./-]+$#', $relative)) {
    http_response_code(400);
    exit('Invalid image path');
}

$attachmentRoot = realpath($root . '/data/attachment');
$source = realpath($root . '/' . $relative);
if (!$attachmentRoot || !$source ||
    strpos($source, $attachmentRoot . DIRECTORY_SEPARATOR) !== 0 || !is_file($source)) {
    http_response_code(404);
    exit('Image not found');
}

$size = @getimagesize($source);
if (!$size || empty($size[0]) || empty($size[1])) output_original_image($source);

$sourceWidth = intval($size[0]);
$sourceHeight = intval($size[1]);
$imageType = isset($size[2]) ? intval($size[2]) : 0;

/* GIF 保留原文件，保证横幅广告动画不会丢失。 */
if ($imageType === IMAGETYPE_GIF || $sourceWidth <= $width ||
    $sourceWidth * $sourceHeight > 20000000 || !function_exists('imagecreatetruecolor')) {
    output_original_image($source);
}

$cacheDir = $root . '/data/attachment/app_thumb/' . $width;
if (!is_dir($cacheDir)) @mkdir($cacheDir, 0755, true);
$cacheFile = $cacheDir . '/' . md5($relative . '|' . @filemtime($source)) . '.jpg';

if (is_file($cacheFile) && filesize($cacheFile) > 0) {
    header('Content-Type: image/jpeg');
    header('Cache-Control: public, max-age=2592000, immutable');
    header('X-Content-Type-Options: nosniff');
    readfile($cacheFile);
    exit;
}

$create = null;
if ($imageType === IMAGETYPE_JPEG && function_exists('imagecreatefromjpeg')) $create = 'imagecreatefromjpeg';
elseif ($imageType === IMAGETYPE_PNG && function_exists('imagecreatefrompng')) $create = 'imagecreatefrompng';
elseif (defined('IMAGETYPE_WEBP') && $imageType === IMAGETYPE_WEBP && function_exists('imagecreatefromwebp')) $create = 'imagecreatefromwebp';

if (!$create) output_original_image($source);
$original = @$create($source);
if (!$original) output_original_image($source);

$targetWidth = $width;
$targetHeight = max(1, intval(round($sourceHeight * $targetWidth / $sourceWidth)));
$thumbnail = imagecreatetruecolor($targetWidth, $targetHeight);
$white = imagecolorallocate($thumbnail, 255, 255, 255);
imagefill($thumbnail, 0, 0, $white);
imagecopyresampled($thumbnail, $original, 0, 0, 0, 0, $targetWidth, $targetHeight, $sourceWidth, $sourceHeight);

header('Content-Type: image/jpeg');
header('Cache-Control: public, max-age=2592000, immutable');
header('X-Content-Type-Options: nosniff');
if (@imagejpeg($thumbnail, $cacheFile, 78)) readfile($cacheFile);
else imagejpeg($thumbnail, null, 78);
imagedestroy($thumbnail);
imagedestroy($original);
