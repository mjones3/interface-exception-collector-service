# Weekly cleanup script
cat > ~/cleanup-tilt-images.sh << 'EOF'
#!/bin/bash
echo "Cleaning up old Tilt images..."
docker exec -it api-governance-control-plane crictl rmi --prune
echo "Current space usage:"
docker exec -it api-governance-control-plane df -h /
EOF

chmod +x ~/cleanup-tilt-images.sh