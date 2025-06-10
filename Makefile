clean:
	rm -rf target

run:
	clj -M:dev

repl:
	clj -M:dev:nrepl

testz:
	clj -M:test

uberjarlight:
	npm run tailwind && clj -T:build all && tput bel
