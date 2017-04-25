import unittest, sys, json, requests

def basic_request(url, params=None, verb="get"):
    headers = {'Authorization': access_token}
    request = requests.request(verb, url, params=params, headers=headers)
    return request

class gateway_tests(unittest.TestCase):

    def test_success(self):
        params = {"page[size]": 1000}
        request = basic_request(base_url,params)
        self.assertEqual(request.status_code, 200)

    def test_links(self):
        request = basic_request(base_url)
        response = request.json()["links"]

        self.assertIsNotNone(response["self"])
        self.assertIsNotNone(response["first"])
        self.assertIsNotNone(response["last"])
        self.assertIsNone(response["prev"])
        self.assertIsNotNone(response["next"])

    def test_instances(self):
        for instance in instances:
            params = {'instance': instance}
            request = basic_request(base_url,params).json()
            for app in request["data"]:
                self.assertEqual(app["id"], app["attributes"]["applicationName"])
                self.assertIsNotNone(app["attributes"]["versions"][instance])

    def test_get_by_id(self):
        url = base_url + "/" + application_id
        request = basic_request(url)
        response = request.json()

        self.assertEqual(request.status_code, 200)
        self.assertEqual(type(response["data"]), dict)
        self.assertEqual(url, response["links"]["self"])
        self.assertEqual(url, response["data"]["links"]["self"])
        self.assertTrue(application_id in response["links"]["self"])
        self.assertTrue(application_id in response["data"]["links"]["self"])

if __name__ == '__main__':
    options_tpl = ('-i', 'config_path')
    del_list = []
    
    for i,config_path in enumerate(sys.argv):
        if config_path in options_tpl:
            del_list.append(i)
            del_list.append(i+1)

    del_list.reverse()

    for i in del_list:
        del sys.argv[i]

    config_data_file = open(config_path)
    config_json = json.load(config_data_file)

    base_url = config_json["hostname"] + config_json["version"] + config_json["api"]

    # Get Access Token
    post_data = {'client_id': config_json["client_id"],
         'client_secret': config_json["client_secret"],
         'grant_type': 'client_credentials'}
    request = requests.post(config_json["token_api"], data=post_data)
    response = request.json()
    access_token = 'Bearer ' + response["access_token"]

    instances = config_json["environments"]
    application_id = config_json["application_id"]

    unittest.main()