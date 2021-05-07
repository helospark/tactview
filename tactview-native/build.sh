# TODO: optionally clean
make -s clean

if [[ "$1" == "-d" ]]
then
  make -s -j$(nproc) debug
  echo "Debug build"
else
  make -s -j$(nproc) release
fi