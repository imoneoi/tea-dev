from django.urls import path
from . import views

urlpatterns = [
    path('hello', views.hello),
    path('login', views.login),
    path('register', views.register),
    path('proc', views.proc),
    path('userdata', views.userdata)
]
