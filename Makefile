clean:
	rm -rf target

run:
	clj -M:dev

repl:
	clj -M:dev:nrepl

test:
	clj -M:test

uberjar:
	pkill java && npm run tailwind && clj -T:build all
