COMPILE_DEPS = CORE_DEPS + KRYO + CLI + [
    "@cassandra_all//jar",
    "@log4j//jar",
    "@logback_core//jar",
    "@logback_classic//jar",
    "@lombok//jar",
    "@deep_netts_core//jar",
    "@commons_config//jar",
    "@commons_lang_332//jar",
    "@json_2017//jar",
    "@javax_visrec_api//jar",
    "@javax_visrec_ri//jar",
    "@xchart//jar",
    "@vd2d//jar",
    "//core/store/serializers:onos-core-serializers",
    "//core/store/primitives:onos-core-primitives",
]

BUNDLES = [
    "//apps/DoublePDDPGRouting:onos-apps-DoublePDDPGRouting",
    "@deep_netts_core//jar",
    "@commons_config//jar",
    "@commons_lang_332//jar",
    "@json_2017//jar",
    "@javax_visrec_api//jar",
    "@javax_visrec_ri//jar",
    "@xchart//jar",
    "@vd2d//jar",
]

EXCLUDED_BUNDLES = [
]

osgi_jar_with_tests(
    karaf_command_packages = ["org.onosproject.DoublePDDPGRouting"],
    deps = COMPILE_DEPS,
)

onos_app(
    category = "Traffic Engineering",
    description = "This appilication has been created by Marwa Kandil",
    excluded_bundles = EXCLUDED_BUNDLES,
    included_bundles = BUNDLES,
    title = "Double pirority DDPG Routing",
    url = "http://onosproject.org",
)
