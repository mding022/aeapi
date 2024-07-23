from publitio import PublitioAPI

publitio_api = PublitioAPI('kPSwwra8kybjtiaH3qL6', '9ZDZTtu5J9i6GA9p3wbidJuRWscsokMi')
publitio_api.create_file(file=open('path/to/file', 'rb'),
                         title='My title',
                         description='My description')

# Note that the file must be opened for binary reading.
# The publitio_api.create_file function will not close the file.
# Therefore, what you will probably want to do most of the time is:

with open('path/to/file.png', 'rb') as f:
    publitio_api.create_file(file=open('path/to/file', 'rb'),
                             title='My title',
                             description='My description')