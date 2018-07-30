echo "Starting Tagmanager build"

# Build frontend components
npm config set registry https://registry.npmjs.org
npm install
npm run build
npm run build-icons

# Scala build & upload to RiffRaff
sbt riffRaffUpload