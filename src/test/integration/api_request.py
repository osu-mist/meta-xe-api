import json, requests

def basic_request(url, access_token, verb="get"):
    headers = {'Authorization': access_token}
    request = requests.request(verb, url, headers=headers)
    return request

def get_image(url, access_token):
    request = basic_request(url, access_token)
    
    try:
        image = Image.open(BytesIO(request.content))
    except IOError:
        image = None

    return (image, request.status_code)