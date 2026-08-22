<?php

// 与其他 API 使用相同的启动方式，禁止 PHP 警告混入 JSON。
error_reporting(0);
ini_set('display_errors', '0');

if (!defined('IN_API')) {
    define('IN_API', true);
}

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

function permission_response($code, $message, $data = array()) {
    echo json_encode(array(
        'code' => intval($code),
        'message' => $message,
        'data' => $data
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

function group_is_in_forum_perm($permissionList, $groupid) {
    $permissionList = trim(str_replace(',', ' ', (string)$permissionList));
    if ($permissionList === '' || $permissionList === '0') {
        return true;
    }
    $groups = preg_split('/\\s+/', $permissionList);
    return in_array((string)intval($groupid), $groups, true);
}

try {
    $fid = intval($_GET['fid'] ?? 0);
    $uid = intval($_GET['uid'] ?? 0);

    if ($fid <= 0) {
        permission_response(400, '板块参数不能为空');
    }

    // 优先从登录 Token 识别用户，不信任客户端传入的 UID。
    $token = trim($_SERVER['HTTP_X_TOKEN'] ?? ($_GET['token'] ?? ''));
    if ($token !== '') {
        $tokenData = C::t('app_token')->get_by_token($token);
        if (!$tokenData || intval($tokenData['expire']) < TIMESTAMP) {
            permission_response(401, '登录已失效，请重新登录');
        }
        $uid = intval($tokenData['uid']);
    }

    if ($uid <= 0) {
        permission_response(401, '请先登录后查看板块权限');
    }

    $forum = DB::fetch_first(
        "SELECT f.fid, f.name, ff.viewperm, ff.postperm
         FROM " . DB::table('forum_forum') . " f
         LEFT JOIN " . DB::table('forum_forumfield') . " ff ON ff.fid = f.fid
         WHERE f.fid = %d AND f.type = 'forum'",
        array($fid)
    );
    if (!$forum) {
        permission_response(404, '板块不存在');
    }

    $member = DB::fetch_first(
        "SELECT uid, groupid, credits FROM " . DB::table('common_member') . " WHERE uid = %d",
        array($uid)
    );
    if (!$member) {
        permission_response(401, '用户不存在');
    }

    $groupid = intval($member['groupid']);
    $credits = intval($member['credits']);
    $group = DB::fetch_first(
        "SELECT g.groupid, g.grouptitle, g.allowvisit, f.allowpost
         FROM " . DB::table('common_usergroup') . " g
         LEFT JOIN " . DB::table('common_usergroup_field') . " f ON f.groupid = g.groupid
         WHERE g.groupid = %d",
        array($groupid)
    );
    if (!$group || intval($group['allowvisit']) <= 0) {
        permission_response(403, '当前用户组无权访问论坛');
    }

    // forum_access 是“单用户特殊权限”表：只按 uid 查询，不按 groupid 查询。
    $access = DB::fetch_first(
        "SELECT allowview, allowpost
         FROM " . DB::table('forum_access') . "
         WHERE fid = %d AND uid = %d",
        array($fid, $uid)
    );

    $allowView = group_is_in_forum_perm($forum['viewperm'], $groupid);
    if ($access && intval($access['allowview']) <= 0) {
        $allowView = false;
    }

    $allowPost = $allowView
        && intval($group['allowpost']) > 0
        && group_is_in_forum_perm($forum['postperm'], $groupid);
    if ($access && intval($access['allowpost']) <= 0) {
        $allowPost = false;
    }

    $groupName = $group['grouptitle'] ?: '普通会员';
    $isVip = in_array($groupid, array(22, 23, 24), true);

    if (!$allowView) {
        permission_response(403, '当前用户组“' . $groupName . '”无权访问“' . $forum['name'] . '”。请升级用户组或开通 VIP。', array(
            'allow' => false,
            'allow_post' => false,
            'groupid' => $groupid,
            'group_name' => $groupName,
            'credits' => $credits,
            'is_vip' => $isVip
        ));
    }

    permission_response(0, $allowPost
        ? '当前用户组“' . $groupName . '”可在“' . $forum['name'] . '”发布主题。'
        : '当前用户组“' . $groupName . '”可浏览该板块，但没有发布主题权限。请升级用户组或开通 VIP。', array(
            'allow' => true,
            'allow_post' => $allowPost,
            'groupid' => $groupid,
            'group_name' => $groupName,
            'credits' => $credits,
            'is_vip' => $isVip
        ));
} catch (Throwable $error) {
    permission_response(500, '权限验证服务异常：' . $error->getMessage());
}
