# Giełda_dyżurowa

Giełda_dyżurowa is a robust web application designed to facilitate the efficient matching of available doctors with hospital duty slots, particularly focusing on overnight shifts. It serves as a dual-platform for hospitals to post their duty requirements and doctors to showcase their availability, streamlining the process to ensure hospitals are never understaffed for overnight duties.

## Overview

The application is architected using Node.js with the Express framework powering the backend, MongoDB for data storage, Mongoose ORM for data modeling, and EJS for server-side templating. The frontend is styled using Bootstrap, with Vanilla JavaScript for dynamic functionalities. The application's structure is modular, with separate routes, models, and views to support its varied features efficiently.

## Features

- **User Registration and Profiles:** Allows hospitals and doctors to create profiles detailing specialties and locations.
- **Duty Slot Publication:** Enables hospitals to post available overnight duty slots.
- **Application for Duties:** Doctors can browse through and apply for these slots or post their availability for hospitals to find them.
- **Profile Viewing:** Facilitates transparency by allowing hospitals and doctors to view each other's profiles.
- **External Communication:** Encourages further communication through external means post duty slot allocation.

## Getting started

### Requirements

- Node.js
- MongoDB
- A modern web browser

### Quickstart

1. Clone the repository to your local machine.
2. Copy `.env.example` to `.env` and fill in your database URL and session secret.
3. Install dependencies with `npm install`.
4. Start the application using `npm start`.
5. Access the application through `http://localhost:3000`.


### Useful commands

Heroku
curl https://cli-assets.heroku.com/install-ubuntu.sh | sh
heroku --version
heroku config:set PORT=3000 -a powerful-sea-67789
heroku config:set DATABASE_URL='mongodb+srv://macius702:Wigolmhmyfn11;@cluster0.g84cahs.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0'  -a powerful-sea-67789 
heroku config:set SESSION_SECRET=e4e634dcb2c84f67afed8dd2c0a61b8c -a powerful-sea-67789
git push heroku master
heroku logs --tail
heroku apps


npm install cypress --save-dev

npm install swagger-jsdoc swagger-ui-express

npm install cookie-parser


### Todo

- [ ]  Look and feel
- [ ] showDatePicker is twice
- [ ] ExposedDropdownMenuBox is twice
- [ ] Test for Register in Kotlin RegisterScreen - onfailure -> snack, GUI test -> delete user needed first


- [ ] some unique index - adding the same duty slot  is ok now! - referential integrity
- [ ] refactor - split 'isAuthenticated'
- [ ] remove doctor availability
- [ ] /duty/find_by_specialty change into general finding endpoint
- [ ] .populate more specific or not needed - use database view - generally send only the relevant information, no extras
- [ ] show only my hospital duty records - or enable buttons only on my duty slots
- [ ] languages
- [ ] const Holidays = require('date-holidays');
- [ ] Kotlin is not sending broadcast on publishDutyVacancy
- [ ] web not sending broadcast on Remove
- [ ] shorten ExposedDropdownMenuBox in DutyVacancyPublishScreen
- [ ] how about       priceFrom: BigDecimal? = null,         currency: Currency? = Currency.PLN, in Lotlin 


- [ ] Doctors should be able to publish their unavailabilities as well
- [ ] select spewcialty with letter(s)?
- [ ] Formatter
- [ ] malignant doctor can occupy all slots, and assign to all duties
- [ ] show internet connection is online - especially on start, before login, etc
- [ ] keyboard handling
- [ ] CI/CD
- [ ] unit tests
- [ ] Specialty master table - the sole source of truth
- [ ] Separate datamodel from routes
- [ ] Assure that cypress tests have always users H1 and Doktor2 


- [x] assure that tests have always a duty entry qqq
- [x] DONE remove duty vacancy 
- [x] DONE gui changes on notification
- [x] DONE Button logic dependent automatically on status
- [x] NOT DOING, https is fine - fix passsword in open text (OAuth2 ?2)
- [x] DONE Remove unnmecessary code
- [x] DONE Format
- [x] DONE Fix look of Refister Kotlin dialog, Register error - toast or snack
- [x] DONE specialty enum
- [x] DONE Missing Fields - price range, PLN, hour
- [x] DONE Swagger
- [x] DONE remove in publish : date: String, dutyHours: String,
- FAILED Publish duty vacancy in Kotlin - don't drag specialtyviewmodel  everywhere, specialties list is enough



### License

Copyright (c) 2024.