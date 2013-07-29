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

### Features
- Upload image to S3 and insert into Markdown editor
- Blog settings (title, description)
- Theme settings (border color, background, custom logo, font)
- Save post as draft
- Add post authors (integrates with gravatar)
- Blog post create/update validation (title must not be empty)
- Pagination

### UX
- Auto-expandable textarea
- Focus mode (hide all other elements during post writing)
- Move all blog edit/create actions to dropdown menu (top right)
- Post date and slug will only be editable if click on it during post edit page