# simpleweb

Simple clojure + clojurescript boilerplate web app.

## Running

    lein run

Visit http://localhost:3000

## Auto-compiling

### Clojurescript
    
    lein cljsbuild auto

### LESS

Use [SimpLESS](http://wearekiss.com/simpless).

## Deploy to Heroku

### Database (Redis)

Add Redis Cloud (free) on Heroku:

    heroku addons:add rediscloud:20