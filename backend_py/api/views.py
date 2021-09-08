from django.shortcuts import render
from django.http import JsonResponse
import json
import requests
from urllib import parse
from .models import GUser
import hashlib
import uuid


# Create your views here.
EDUKG_ID = ""
API_KEY = "teadevkey0"

HEADERS = {
    "Content-Type": "application/x-www-form-urlencoded;charset=utf-8"
}


def get_user_by_sess(session: str):
    db_user = list(GUser.objects.filter(session=session))
    if db_user:
        return db_user[0]
    else:
        return None


def hello(request):
    return JsonResponse({
        "data": "hello, world!"
    })


def login(request):
    post_json = json.loads(request.body)
    user = post_json.get("user", "")
    pw = post_json.get("passwd", "")
    db_user = GUser.objects.filter(username=user)
    db_user = list(db_user)
    if db_user and db_user[0].password == hashlib.sha1(pw.encode("utf-8")).hexdigest():
        db_user[0].session = uuid.uuid4()
        db_user[0].save()
        return JsonResponse({
            "ok": 1,
            "session": db_user[0].session,
            "msg": f"login succeeded, user: {user}, pw: {pw}"
        })
    else:
        return JsonResponse({
            "ok": 0,
            "session": "",
            "msg": "login failed"
        })


def register(request):
    post_json = json.loads(request.body)
    user = post_json.get("user", "")
    pw = post_json.get("passwd", "")
    data = post_json.get("data", "")
    if user and pw:
        db_user = GUser(username=user, password=hashlib.sha1(pw.encode("utf-8")).hexdigest(), data=data, session=uuid.uuid4())
        db_user.save()
        return JsonResponse({
            "ok": 1,
            "msg": f"register succeeded, user: {user}, pw: {pw}, data: {data}"
        })
    else:
        return JsonResponse({
            "ok": 0,
            "msg": "register failed"
        })


def userdata(request):
    post_json = json.loads(request.body)
    sess = post_json.get("session", "")
    data = post_json.get("data", "")
    db_user = get_user_by_sess(sess)
    if db_user:
        if data:
            db_user.data = data
            db_user.save()
            return JsonResponse({
                "ok": 1,
                "data": data
            })
        else:
            return JsonResponse({
                "ok": 1,
                "data": db_user.data
            })
    else:
        return JsonResponse({
            "ok": 0,
            "data": ""
        })


def __login():
    global EDUKG_ID
    url = "http://open.edukg.cn/opedukg/api/typeAuth/user/login"
    data = {
        "phone": 15538088958,
        "password": "Vg0jhog4SG461VhXx7Zq"
    }
    res = requests.post(url, data=parse.urlencode(data), headers=HEADERS)
    EDUKG_ID = json.loads(res.text).get("id", "")


def __get_res(url, method, data):
    global EDUKG_ID
    data["id"] = EDUKG_ID
    if method == "GET":
        res = requests.get(url, params=data, headers=HEADERS)
    elif method == "POST":
        res = requests.post(url, data=parse.urlencode(data), headers=HEADERS)
    return json.loads(res.text)


def proc(request):
    global EDUKG_ID
    post_json = json.loads(request.body)
    key = post_json.get("key", "")
    url = post_json.get("url", "")
    method = post_json.get("method", "")
    data = json.loads(post_json.get("data", ""))
    print(key, url, method)
    if key == API_KEY and url and method:
        print("ok", data)
        json_res = __get_res(url, method, data)
        if json_res.get("code", "-1") == "-1":
            __login()
            json_res = __get_res(url, method, data)
        return JsonResponse(json_res)
    else:
        return JsonResponse({})