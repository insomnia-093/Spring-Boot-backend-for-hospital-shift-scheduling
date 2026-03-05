import json
import requests
import os
from dotenv import load_dotenv
load_dotenv()
coze_api_token = os.getenv('coze_api_token')
session_id = os.getenv('session_id')
project_id = os.getenv('project_id')
query_text = "你好啊。"
url = "https://k5k4342js5.coze.site/stream_run"
headers = {
  "Authorization": "Bearer " + coze_api_token,
  "Content-Type": "application/json",
  "Accept": "text/event-stream",
}
payload = {
    "content": {
        "query": {
            "prompt": [
                {
                    "type": "text",
                    "content": {
                        "text": query_text
                    }
                }
            ]
        }
    },
    "type": "query",
    "session_id": session_id,
    "project_id": project_id
}
response = requests.post(url, headers=headers, json=payload, stream=True)
print("status:", response.status_code)
try:
  response.raise_for_status()
except Exception:
  print(response.text)
  raise
for line in response.iter_lines(decode_unicode=True):
  if line and line.startswith("data:"):
    data_text = line[5:].strip()
    try:
      parsed = json.loads(data_text)
      print(json.dumps(parsed, ensure_ascii=False, indent=2))
    except Exception:
      print(data_text)