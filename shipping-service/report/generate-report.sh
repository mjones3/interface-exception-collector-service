rm -rf allure-results/history
mkdir -p allure-results/history
cp allure-report/history/* allure-results/history

# Get the operating system name
os_name=$(uname -s)
# Get the operating system version
os_version=$(uname -r)
# Get the machine hardware name
machine_hardware=$(uname -m)

echo "os_platform:$os_name\nos_version:$os_version\nmachine_hardware:$machine_hardware" > allure-results/environment.properties
echo "{\"type\":\"gitlab\", \"url\":\"$CI_PROJECT_URL\", \"name\":\"$CI_PROJECT_NAME\", \"build\":\"$CI_PIPELINE_ID\"}" > allure-results/executor.json

allure generate --clean
allure open
