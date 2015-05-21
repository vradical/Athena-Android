# 1. User Story #

* Users can be warned of dangerous area. (Covered)
* Users can make use of the application without connectivity.
* Users can still know dangerous area without connectivity.
* Users can still track their location without connectivity.
* Users can still use high alert without connectivity
* Users can still trigger and send out emergency alert without connectivity.
* Users can be on high alert to be ready for any form of assault. (Covered)
* High alert should be able to activate the emergency alert if the users has no response. (Covered)
* Users can easily trigger the emergency alert at anywhere with just 1 step. (Covered)
* Users can make use of the emergency alert to scare off the attacker. (Covered)
* The system can automatic can capture and send out vital information in the case of an assault. (Covered)
* Users can pre-set the numbers of NOK in the application. (Covered)
* The system can automatic send out help signals to nearby users. (Covered)
* Users can cancel off the emergency alert. (Covered)
* Users can choose between to cancel because of false alarm or they are safe again. (Covered)
* Users will have access to vital information such as nearest hospital or local authority numbers when in need. (Covered)
* Users can edit the time and range of the tracking based on their preference. (Covered)

# 2. Features #

## 2.1. Prevention ##
### 2.1.1. Danger Zone ###
* Able to view last incident within the area (To know the level of crime?)
* Danger level is calculate through an algorithm based on N level(1 <= N <= 10)
* Algorithm is based on number emergency activated at specific location
* The records is based on N number of activation, with each activation to be N+1 level.
* After each month without an incident, N will be N-1.
* Map will be displayed in the form of geographic information system (layers of red overlapping,   symbolising the dangerous level)
* Notification will be given to the user when they have enter a more dangerous area (based on the periodic tracking)

### 2.1.2. Tracking System ###
* Similar to taxi system, the tracking is done periodically by both range and time. (Can be adjust by users through advance option)
* Different mode for tracking (impt?)

### 2.1.3. Help Information Display ###
* Help information such as nearest hospital and police will be given to the users in this page

### 2.1.4. FB Sharing ###
* Allow user to share the app with their friends.

## 2.2. Emergency ##
### 2.2.1. High Alert Mode ###
* Tracking are done more frequently.
* Vibration to prompt if users are okay with each have 1 min duration for user to answer.
* If users has failed to response 2 times consecutively, emergency alert will be triggered.

### 2.2.2. Emergency Mode ###
* 1 step to activate the emergency mode (HOW?)
* Send alert via SMS and email to preset NOK with no limitation (with country priority).
* Location are tracked and send
* Voice Recording are activated for 5-30 sec? and send
* Photo will be taken and send as well.
* Local authority will be provided in the mail for fast response.
* SOS alert will be send to nearby users within a specific range (To be release at later stage - when there are community)
* Siren will be activated with speaker mode on loudest volume
* In the case of no connectivity,
* detailed are stored until connectivity available again and send out.
* last recorded location send via sms. (or can android detect location without internet?)

## 2.3. Post-Incident ##
* Cancellation of Emergency Mode
* Cancel due to False alarm
* Notify NOK and nearby ppl that it was a false alarm
* Record of the activation to be deleted.
* Cancel due to Safe again
* Information of nearest hospital and local authority will be provided.
* System will not notify NOK and nearby ppl (users might still need help).

# 3. Stages of Development #

## 3.1. First Stage (Without web services) ##
* Tracking System [High]
* High Alert Mode [Medium]
* Emergency Alert (Without no connection feature & SOS to nearby users) [Medium]
* Help information Page [Medium]
* Basic Cancellation (Without choices) [Low]

## 3.2. Second Stage (With web services to store tracking info) ##
* User Registration w/ Facebook & Google [High]
* Emergency Alert (With SOS to nearby users) [Low]
* Cancellation of Emergency Mode (With the two choices) [Medium]
* Notification System to Android (Ask user for response / running in background) [Low]
* Facebook Sharing [Low]

## 3.3. Third Stage (GIS) ##
* Danger zone features w/ google map. [High]
* Wearable Tech [Medium]
* Backend admin functions (by front end website) [Low]
* Solve all connectivity issues. [Low]


# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact