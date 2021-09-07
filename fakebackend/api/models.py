from django.db import models

# Create your models here.


class GUser(models.Model):
    username = models.CharField(max_length=50)
    password = models.CharField(max_length=200)
    data = models.CharField(max_length=1000)
    session = models.CharField(max_length=200)
