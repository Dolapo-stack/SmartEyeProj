"""
 This file contains the database model written with Object Relational Mapper
"""
from base import db, app
from datetime import datetime

emergency_contacts = db.Table(
    'emergency_contacts',
    db.Column('user_id', db.Integer(), db.ForeignKey('users.id')),
    db.Column('contact_id', db.Integer(), db.ForeignKey('contacts.id'))
)

class Contact(db.Model):
    __tablename__ = 'contacts'

    id = db.Column(db.Integer, primary_key=True)
    fullname = db.Column(db.String(120))
    phone_no = db.Column(db.String(15))
    email_address = db.Column(db.String(255))

    def __init__(self, fullname, phone_no, email_address):
        self.fullname = fullname
        self.phone_no = phone_no
        self.email_address = email_address

class User(db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    firstname = db.Column(db.String(20))
    lastname = db.Column(db.String(20))
    phone_no = db.Column(db.String(15))
    email_address = db.Column(db.String(255))
    dob = db.Column(db.String(255))
    gender = db.Column(db.String(10))
    password = db.Column(db.String())
    panic_status = db.Column(db.String(), default="NORMAL")
    contacts = db.relationship('Contact', secondary=emergency_contacts,
                            backref=db.backref('users', lazy='dynamic'))

    def __init__(self, firstname, lastname, phone_no, email_address, gender, dob, password):
        self.firstname = firstname
        self.lastname = lastname
        self.phone_no = phone_no
        self.email_address = email_address
        self.gender = gender
        self.dob = dob
        self.password = password


class PanicHistory(db.Model):
    __tablename__ = 'panic_history'

    id = db.Column(db.Integer, primary_key=True)
    fullname = db.Column(db.String(120))
    cood_lat = db.Column(db.String())
    cood_long = db.Column(db.String())
    status = db.Column(db.String())
    user_id = db.Column(
        db.Integer, db.ForeignKey("users.id", ondelete="CASCADE"), index=True
    )
    time_created = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)

    def __init__(self, fullname, cood_long, cood_lat, user_id, status):
        self.fullname = fullname
        self.cood_long = cood_long
        self.cood_lat = cood_lat
        self.user_id = user_id
        self.status = status

class Tip(db.Model):
    __tablename__ = 'tips'

    id = db.Column(db.Integer, primary_key=True)
    description = db.Column(db.String(255))

    def __init__(self, firstname):
        self.description = description