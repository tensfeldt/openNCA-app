baseUrl="http://localhost:8080/modeshape-rest/sample/default"
username="admin"
password="admin"

# Change to the directory where the script is:
cd $(dirname $0)

exit
# Create base directory under /items
curl --request POST \
  --url "$baseUrl/items/artifacts" \
  --user "$username:$password" \
  --header 'Content-Type: application/json' \
  --data '{ "jcr:primaryType": "nt:folder" }'

# Import custom node type
curl --request POST \
  --url $baseUrl/nodetypes \
  --user "$username:$password" \
  --header 'Content-Type: multipart/form-data' \
  --form 'file=@equip-library-artifacts.cnd'
