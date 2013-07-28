# Hanjuku

Ridiculously minimal blog engine, written in Clojure.

## Running

    lein run

Visit http://localhost:3000

## Auto-compiling

### Clojurescript
    
    lein cljsbuild auto

### LESS

Use [SimpLESS](http://wearekiss.com/simpless).

## TODO
- Upload image to S3 and insert into Markdown editor
- Blog settings (title, description)
- Theme settings (border color, background, custom logo, font)
- Auto-expandable textarea
- Save post as draft
- Add post authors (integrates with gravatar)
- Auto-generate post date, editable in post edit page
- Focus mode (hide all other elements during post writing)
- Blog post create/update validation (title must not be empty)
- Display post in right order (reverse chronological) on front page