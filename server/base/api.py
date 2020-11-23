'''this file contains api methods for the api routes
'''
from flask import request, jsonify, flash, url_for, redirect, session, g
from flask_jwt_extended import \
    create_access_token, jwt_required, get_jwt_identity
from flask_restful import Resource

from flask_mail import Message

from base import app, db, mail, client, pwd_context
from base.models import User, Contact, PanicHistory

# Methods used for encrypting and comparing password hash in database
# Reference: https://blog.tecladocode.com/learn-python-encrypting-passwords-python-flask-and-passlib/
def encrypt_password(password):
    return pwd_context.encrypt(password)

def check_encrypted_password(password, hashed):
    return pwd_context.verify(password, hashed)

def send_email(email_subject, sender_email, receiver_email, email_body):
    ''' send email method
    contains subject of email, receivers, body and sender's email
    used for sending emails using Flask-Mail package
    Refrence: https://pythonhosted.org/flask-mail/
    '''
    try:
        msg = Message(email_subject, sender=sender_email, recipients=[receiver_email])
        msg.body = email_body
        mail.send(msg)
        return 'Email sent!'
    except Exception as e: # throws an error if email could not be sent
        return(str(e))

def send_sms(sms_body, sender_phone, receiver_phone):
    ''' Send SMS to emergency contacts using Twilio API service
    Reference: https://www.twilio.com/docs/libraries/python
    '''
    return client.messages.create(
        from_="+18286004935", # twilio trial no.
        to=receiver_phone, 
        body=sms_body
    )

class PanicStatusApi(Resource):
    '''
    this method simply checks if the api server is
    functional or online'''
    def get(self):
        req = request.args.get('user_id')

        user_id = req

        user = User.query.get(user_id)
        if not user:
            return jsonify(message="user not found")
        if user.panic_status == "PANIC":
            _message="Help!\n%s is currently in panic mode."%(user.firstname)
        else:
            _message="All Good! %s is safe!"%(user.firstname)
        return jsonify(
            status=user.panic_status, 
            message=_message
        )

class LoginApi(Resource):
    def post(self):
        '''
        get user phone_no and password from client/smartwatch
        then check database if correct
        '''
        req = request.form

        phone_no = req.get('phone_no')
        password = req.get('password')

        print(phone_no)
        print(password)

        user=User.query.filter_by(phone_no=phone_no).first() 
        if not user or not check_encrypted_password(password, user.password):
            print("user not registered!")
            return jsonify(status=401, message="user not registered!")

        print("user success!")

        return jsonify(
            status="SUCCESS",
            token="111",
            id=int(user.id),
            panic_status=user.panic_status,
            name="%s %s"%(user.firstname, user.lastname),
            #user=str(user),
            message="Welcome %s"%(user.firstname)
        )

class LoginWebApi(Resource):
    def post(self):
        '''
        This api is strictly for web login
        get user phone_no and password from client
        then check database if correct
        '''
        req = request.form

        phone_no = req.get('phone_no')
        password = req.get('password')

        print(phone_no)
        print(password)

        user=User.query.filter_by(phone_no=phone_no).first() 
        if not user or not check_encrypted_password(password, user.password):
            flash("incorrect phone no or password!")
            return redirect(url_for('login_page'))
        
        # store user id in session
        session['user_id'] = user.id

        flash("Successfully logged in. Add emergency contacts")
        return redirect(url_for('register_contacts'))

class RegisterApi(Resource):
    def post(self):
        req = request.form

        firstname = req.get('firstname')
        lastname = req.get('lastname')
        phone_no = req.get('phone_no')
        email = req.get('email')
        gender = req.get('gender')
        dob = req.get('DOB')
        password = req.get('password')

        user = User.query.filter_by(phone_no=phone_no).first()
        if user:
            flash("you are already registered. log in on smartwatch app")
            return redirect(url_for('register_page'))
            #return jsonify(status=401, message="That user already exist!")
        
        # encrypt and store password hash ONLY in the database
        hashed_password = encrypt_password(password)
        
        new_user = User(
            firstname, 
            lastname, 
            phone_no, 
            email, 
            gender,
            dob,
            hashed_password
        )

        db.session.add(new_user)
        db.session.commit()

        # store user id in session
        session['user_id'] = new_user.id

        flash("Successfully registered. Add emergeency contacts")
        return redirect(url_for('register_contacts'))

        # return jsonify(
        #     status=200,
        #     message="User successfully registered",
        #     user=new_user
        # )

class RegisterContactApi(Resource):
    def post(self):
        req = request.form

        # input contact details
        contact_fullname = req.get('contact_fullname')
        contact_phone_no = req.get('contact_phone_no')
        contact_email = req.get('contact_email')

        if not g.user:
            return jsonify(status=401, message="user not found!")
        
        # compare current logged in user phone with submitted contact phone
        if g.user.phone_no == contact_phone_no:
            return jsonify(status=401, message="you cannot add yourself as emergency contact")
        
        # create new contact and add to database
        new_contact = Contact(
            contact_fullname,
            contact_phone_no,
            contact_email
        )

        # save contact to database
        db.session.add(new_contact)
        db.session.commit()

        # get "new_contact" id and saves it in the "emergency_contacts" table
        g.user.contacts.append(new_contact)
        db.session.commit()

        flash("emergency contact added!")
        return redirect(url_for('register_contacts'))

        # return jsonify(
        #     status=200,
        #     new_contact_name=new_contact.fullname,
        #     new_contact_phone=new_contact.phone_no,
        #     message="emergency contact added!"
        # )

class ActivatePanicApi(Resource):
    def post(self):
        '''
        get user unique id for activating
        '''
        req = request.form
        user_id = int(req['user_id'])
        longitude = req['long']
        latitude = req['lat']

        print(user_id)
        print(longitude)
        print(latitude)

        user = User.query.filter_by(id=user_id).first()
        if not user:
            print("User not found!")
            return jsonify(status=401, message="User not found!")
        
        # change panic status of user
        user.panic_status = "PANIC"
        db.session.commit()

        # convert location coordinates sent from smartwatch to google maps link
        # https://www.google.com/maps/search/?api=1&query=<lat>,<lng>
        friendly_location = 'https://www.google.com/maps/search/?api=1&query=%s,%s'%(latitude, longitude)
        user_fullname= "%s %s"%(user.firstname, user.lastname)
        HELP_MESSAGE = "Hi, \nUrgent! %s is in danger at this location %s\n Come and help!"%(user_fullname, friendly_location)
        print(HELP_MESSAGE)

        # create panic history
        # and store GPS coordinates and last seen time in database
        # status in panic history is "PANIC"
        panic_history = PanicHistory(
            user_fullname, longitude, latitude, user.id, "PANIC"
        )

        db.session.add(panic_history)
        db.session.commit()

        # loop through registered contacts
        # send notification messages to emergency contacts email adddress
        my_contacts = user.contacts
        if my_contacts:
            for contact in my_contacts:
                mail = send_email("Break Free app emergency", user.email_address, contact.email_address, HELP_MESSAGE)
                print(mail)
                sms = send_sms(HELP_MESSAGE, user.phone_no, contact.phone_no)
                print(sms)

        return jsonify(
            status=200,
            message="Panic mode activated! Sending messages to emergency contacts",
            data=HELP_MESSAGE
        )

class CancelPanicApi(Resource):
    def post(self):
        '''
        get user unique id for de-activating
        '''
        req = request.form
        user_id = int(req['user_id'])
        longitude = req['long']
        latitude = req['lat']

        print(user_id)
        print(longitude)
        print(latitude)

        user = User.query.filter_by(id=user_id).first()
        if not user:
            print("User not found!")
            return jsonify(status=401, message="User not found!")
        
        # change panic status of user
        user.panic_status = "NORMAL"
        db.session.commit()

        user_fullname= "%s %s"%(user.firstname, user.lastname)

        # change panic history to normal
        # and store GPS coordinates and last seen time in database
        panic_history = PanicHistory(
            user_fullname, longitude, latitude, user.id, "NORMAL"
        )

        db.session.add(panic_history)
        db.session.commit()

        # send notifications to contacts saying
        # this person is no longer in danger
        SAFE_MESSAGE = "Thank you! %s is no longer in danger and has probably received assistance." %(user_fullname)
        my_contacts = user.contacts
        if my_contacts:
            for contact in my_contacts:
                mail = send_email("Break Free app emergency", user.email_address, contact.email_address, SAFE_MESSAGE)
                print(mail)
                sms = send_sms(SAFE_MESSAGE, user.phone_no, contact.phone_no)
                print(sms)

        return jsonify(
            status=200,
            message="Panic mode canceled!",
            data=SAFE_MESSAGE
        )