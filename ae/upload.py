from publitio import PublitioAPI
import sys

path = sys.argv[0]
name = sys.argv[1]

publitio_api = PublitioAPI('9r6BXVgQ91jqqvXlvyJB', '7VWkrq52G1wQMrgRrexvfmfcJmpcqBB8')
with open(path, 'rb') as f:
    publitio_api.create_file(file=open(path, 'rb'),
                             title=name,
                             description='Uploaded by aeapi')