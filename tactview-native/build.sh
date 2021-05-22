# TODO: optionally clean
make -s clean

if [[ "$1" == "-r" ]]
then
  make -s -j$(nproc) release
else
  make -s -j$(nproc) debug
  echo "Debug build"
fi