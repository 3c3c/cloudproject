package com.cloud.common.handler;

/**
 * 当前操作人提供者 SPI。
 *
 * <p>解耦 cloud-common 与 Spring Security：审计字段填充器
 * （{@link MybatisPlusMetaObjectHandler}）需要拿到当前登录用户名，
 * 但 cloud-common 不应强制依赖 security。
 * <ul>
 *   <li>cloud-common 提供默认实现 {@link DefaultCurrentUserProvider}，返回 {@code "system"}；</li>
 *   <li>cloud-common-security 提供覆盖实现，委托给 {@code SecurityUtils.getCurrentUsername()}。</li>
 * </ul>
 * 需要鉴权能力的服务依赖 security 模块后，覆盖实现以 {@code @ConditionalOnMissingBean} 优先生效。</p>
 */
public interface CurrentUserProvider {

    /**
     * 获取当前登录用户名，用于审计字段（createdBy / updatedBy）自动填充。
     *
     * @return 当前用户名；无法获取时返回 {@code null}，由调用方决定回退值
     */
    String getCurrentUsername();
}
