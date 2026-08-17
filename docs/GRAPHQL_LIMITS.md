# Límites de complejidad y profundidad en GraphQL

El **rate limit por petición** (ver [`RATE_LIMITING.md`](RATE_LIMITING.md)) no
basta para GraphQL: una sola petición puede ser abusiva por lo **caro** que es
resolverla (anidación profunda, seleccionar muchos campos → consultas N+1).

Por eso además limitamos la **profundidad** y la **complejidad** de cada query.

## Depth (profundidad)

Mide cuántos niveles de anidación tiene la query. Ejemplo con profundidad 3:

```graphql
{
  findAllPackages {            # 1
    status {                   # 2
      name                     # 3
    }
  }
}
```

Una query anidada 50 niveles (posible con tipos que se referencian entre sí) es
un riesgo. `MaxQueryDepthInstrumentation(maxDepth)` la rechaza.

## Complexity (complejidad)

Mide el **costo total** de la query: cada campo suma puntos según su coste, y se
multiplica por el tamaño de las listas. Una query que selecciona cientos de
campos en una lista grande supera el tope aunque sea "poco profunda".

`MaxQueryComplexityInstrumentation(maxComplexity)` usa un
`SimpleFieldComplexityCalculator` (cada campo vale 1 y el tamaño de lista se
estima en 10).

## Configuración

Definido en `config/GraphQlLimitsConfig` (un `GraphQlSourceBuilderCustomizer`
que registra ambas instrumentations):

```java
return builder -> builder.instrumentation(List.of(
        new MaxQueryDepthInstrumentation(properties.getGraphqlMaxDepth()),
        new MaxQueryComplexityInstrumentation(properties.getGraphqlMaxComplexity())));
```

Valores por defecto (ajustables en `application.properties`):

```properties
app.rate-limit.graphql-max-depth=10
app.rate-limit.graphql-max-complexity=100
```

Cuando se supera un tope, la query devuelve un error GraphQL con mensaje del
tipo *"maximum query depth exceeded"* o *"maximum query complexity exceeded"*.

## Cuándo ajustar los valores

- Sube `graphql-max-depth` si el schema gana tipos más anidados legítimos.
- Sube `graphql-max-complexity` si aparecen queries legítimas más anchas
  (muchos campos). Bájalo si quieres ser más estricto.
