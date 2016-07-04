bash run.sh --target=assembly --opt=all $1

method=$2
if [ "$method" == "" ];
then
    method=main
fi

echo CFG of $method

dot -Tpng $method.dot -o $method.png
open $method.png

