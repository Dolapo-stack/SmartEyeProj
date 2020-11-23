from base import app

# run werkzeug server on localhost port 5000
# set server to debug mode
app.debug = True
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)
    
    