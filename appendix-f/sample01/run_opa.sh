docker run --mount type=bind,source="$(pwd)"/policies,target=/policies \
-p 8181:8181 openpolicyagent/opa:0.29.0 run /policies --server
