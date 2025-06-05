
import requests
import json
from pprint import pprint

mass1 = 1800
mass2 = 1800.02
url = f"http://localhost:7071/search-taxonomy?mass1={mass1}&mass2={mass2}&pageSize=10000000"


response = requests.get(url)
if response.status_code == 200:
    data = response.json()
    pprint(data)
    # Parse the results
    results = data.get('result', [])
    parsed_results = []
    for item in results:
        for mass, sequences in item.items():
            for seq_info in sequences:
                parsed_results.append({
                    'mass': mass,
                    'seq': seq_info['seq'],
                    'accsTax': seq_info['accsTax']
                })
    # Print parsed results
    for result in parsed_results:
        print(f"Mass: {result['mass']}, Seq: {result['seq']}, AccsTax: {result['accsTax']}")
else:
    print(f"Error: {response.status_code}, {response.text}")
