{
  description = "bespoke-freemind-ce: reference environment for build and test";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { self, nixpkgs }:
    let
      systems = [ "x86_64-linux" "aarch64-linux" "aarch64-darwin" ];
      forAll = f: nixpkgs.lib.genAttrs systems (s: f nixpkgs.legacyPackages.${s});
    in {
      # Build and test run inside this shell, locally and in CI:
      #   nix develop -c ./gradlew test --no-configuration-cache
      devShells = forAll (pkgs: {
        default = pkgs.mkShell {
          packages = [ pkgs.jdk21 ]
            ++ pkgs.lib.optionals pkgs.stdenv.hostPlatform.isLinux [
              pkgs.xvfb-run
              pkgs.fontconfig
              pkgs.dejavu_fonts
            ];
          # Unicode file names in the tests need a UTF-8 locale. Nix glibc
          # provides C.UTF-8. Java GUI tests also need a font on Linux.
          shellHook = ''
            export LANG=C.UTF-8
            export LC_ALL=C.UTF-8
          '' + pkgs.lib.optionalString pkgs.stdenv.hostPlatform.isLinux ''
            export FONTCONFIG_FILE=${pkgs.makeFontsConf { fontDirectories = [ pkgs.dejavu_fonts ]; }}
          '';
        };
      });
    };
}
