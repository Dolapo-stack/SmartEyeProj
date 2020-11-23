'''
Core server file
I used the flask micro-framework module and flask_restful for building API interfaces
 Author: Oluwaseun Akindolire
'''
import os
from flask import Flask, render_template, g, redirect, url_for, session
from flask_restful import Api
from flask_sqlalchemy import SQLAlchemy
from flask_mail import Mail # package for sending emails

app = Flask(__name__) # initialize flask object to use in application
app.secret_key = b'010101010'

db = SQLAlchemy()
db.init_app(app)

# To setup app logger
log = app.logger

# Database connector
DATABASE_URL = 'postgresql://postgres:smarteye@localhost/smarteye' 
app.config['SQLALCHEMY_DATABASE_URI'] = DATABASE_URL
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# EMAIL configuration
# use Google mail server for sending emails
# requires: gmail address and gmail password
# reference: https://myaccount.google.com/lesssecureapps
app.config.update(
	MAIL_SERVER='smtp.gmail.com',
	MAIL_PORT=465,
	MAIL_USE_SSL=True,
	MAIL_USERNAME = "lilonaony@gmail.com",
	MAIL_PASSWORD = "MrOlaoluwa22"
)

# initialize mail package
mail = Mail(app)

# set up twilio sms API
# and credentials
from twilio.rest import Client

account_sid = "AC0804e0ee2150e57b30e891659201964c"
auth_token = "68763765e4e52bb860c560afa680def0"

client = Client(account_sid, auth_token)

# Use 'passlib' for password encryption
# md5 cryptography algo
from passlib.context import CryptContext
pwd_context = CryptContext(
        schemes=["md5_crypt"],
        default="md5_crypt"
)

# initialize API class provided by flask_restful
from base.api import (
    LoginApi, LoginWebApi, PanicStatusApi, RegisterApi, 
    RegisterContactApi, ActivatePanicApi, CancelPanicApi
)

api = Api(app)

# api routes
api.add_resource(LoginApi, '/api/login')
api.add_resource(LoginWebApi, '/api/login_web')
api.add_resource(RegisterApi, '/api/register')
api.add_resource(RegisterContactApi, '/api/register_contact')
api.add_resource(PanicStatusApi, '/api/panic/status')
api.add_resource(ActivatePanicApi, '/api/panic/activate')
api.add_resource(CancelPanicApi, '/api/panic/cancel_panic')

from base.models import User

@app.before_request
def before_request():
    g.user = None
    if 'user_id' in session:
        g.user = User.query.get(session['user_id'])

@app.route("/")
def index():
    return render_template("home.html")

# This templates are to mimic the fuctionality in place of a smartwatch/phone
@app.route("/login")
def login_page():
    if g.user:
        return redirect(url_for('register_contacts'))
    return render_template("login.html")

@app.route("/register")
def register_page():
    if g.user:
        return redirect(url_for('register_contacts'))
    return render_template("user_registration.html")

@app.route("/add_contacts")
def register_contacts():
    if not g.user:
        return redirect(url_for('login_page'))
    return render_template("emergency_contacts.html")

@app.route(r'/logout', methods=['GET', 'POST'])
def logout():
  session.pop('user_id', None)
  print('Successfully logged out')
  return redirect(url_for('index'))
