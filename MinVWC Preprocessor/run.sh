graph_dir="benchmark_graphs/"
# input_dir="test_input_graphs/"
# input_dir="original_graphs/"
# input_dir="citeseerX"
# input_dir="AstroPh"
# input_dir="as-skitter"
# input_dir="DSJC"
# input_dir="queen" # not applicable
# input_dir="le" # applicable
# input_dir="miles" # applicable
# input_dir="mulsol" # applicable
# input_dir="zeroin" # applicable
# input_dir="wap" 
# input_dir="flat" 
# input_dir="Latin"
input_dir="bio-yeast"

output_dir=$input_dir"_output_graphs/"

java MinWGCPreprocessing $graph_dir$input_dir $output_dir
# java MinWGCPreprocessing $jinkao_input_dir $output_dir
