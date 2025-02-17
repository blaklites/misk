package misk.web.dashboard

import misk.inject.KAbstractModule
import misk.web.WebActionModule
import misk.web.proxy.WebProxyAction
import misk.web.proxy.WebProxyEntry
import misk.web.resources.StaticResourceAction
import misk.web.resources.StaticResourceEntry
import wisp.deployment.Deployment

/**
 * Declare how to serve the resources for a [WebTab] (JS, HTML, CSS...)
 *
 * In Development environment, requests are proxied through to a local running build server.
 * In all other environments, resource requests are served from the classpath.
 *
 * @property isDevelopment is true if this deployment is in development environment
 *   * In Development environment, requests are proxied through to a local running build server.
 *   * In all other environments, resource requests are served from the classpath.
 * @property slug A unique slug to identify the tab namespace, it must match the tab's corresponding
 *   [DashboardTab] multibinding
 * @property web_proxy_url a fully qualified url for the development build server (includes `http://` prefix)
 * @property url_path_prefix URL namespace used to determine where to route requests with that url prefix
 *    By Misk-Web convention, the prefix for tabs is `/_tab/{slug}/` to prevent namespace collisions with
 *    dashboard urls such as `/_admin/` or `/app/` since path routing is by url prefix.
 * @property resourcePath JVM path for non-Development environment static resources (includes `classpath:/` prefix)
 */
class WebTabResourceModule(
  private val isDevelopment: Boolean = false,
  val slug: String,
  val web_proxy_url: String? = null,
  val url_path_prefix: String = "/_tab/$slug/",
  val resourcePath: String = "classpath:/web/_tab/$slug/"
) : KAbstractModule() {

  constructor(
    deployment: Deployment,
    slug: String,
    web_proxy_url: String,
    url_path_prefix: String = "/_tab/$slug/",
    resourcePath: String = "classpath:/web/_tab/$slug/"
  ) : this(deployment.isLocalDevelopment, slug, web_proxy_url, url_path_prefix, resourcePath)

  override fun configure() {
    // Environment Dependent WebProxyAction or StaticResourceAction bindings
    multibind<StaticResourceEntry>()
      .toInstance(
        StaticResourceEntry(url_path_prefix = url_path_prefix, resourcePath = resourcePath)
      )
    if (isDevelopment) {
      web_proxy_url?.let {
        install(WebActionModule.createWithPrefix<WebProxyAction>(url_path_prefix = url_path_prefix))
        multibind<WebProxyEntry>().toInstance(
          WebProxyEntry(url_path_prefix = url_path_prefix, web_proxy_url = web_proxy_url)
        )
      }
    } else {
      install(
        WebActionModule.createWithPrefix<StaticResourceAction>(url_path_prefix = url_path_prefix)
      )
    }
  }
}
