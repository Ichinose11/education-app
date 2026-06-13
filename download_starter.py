import urllib.request
import urllib.parse
import tarfile
import os

url = "https://start.spring.io/starter.tgz"
# Read params from params.txt
with open('/home/Anand9401/user-dashboard-app/params.txt', 'r') as f:
    params_str = f.read().strip()

data = urllib.parse.parse_qs(params_str)
# parse_qs returns lists, convert back to single values
data = {k: v[0] for k, v in data.items()}
encoded_data = urllib.parse.urlencode(data).encode('utf-8')

dest_path = '/home/Anand9401/user-dashboard-app/starter.tgz'

req = urllib.request.Request(url, data=encoded_data, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as response, open(dest_path, 'wb') as out_file:
    out_file.write(response.read())

print("Downloaded starter.tgz")

with tarfile.open(dest_path, "r:gz") as tar:
    tar.extractall(path='/home/Anand9401/user-dashboard-app')
print("Extracted successfully")

os.remove(dest_path)
