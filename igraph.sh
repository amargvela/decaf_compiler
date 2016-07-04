bash run.sh --target=assembly --opt=all $1

method=$2
if [ "$method" == "" ];
then
    method=main
fi

echo Interference graph of $method

dot -Tpng ${method}_color.dot -o ${method}_color.png
open ${method}_color.png

