pgrep -f "java.*gradle.*" | xargs kill -9
rm *.hprof
